import {encodeBase32LowerCaseNoPadding, encodeHexLowerCase} from "@oslojs/encoding";
import {sha256} from "@oslojs/crypto/sha2";

import type {User} from "./user";
import type {RequestEvent} from "@sveltejs/kit";
import type {PrismaClient} from "@prisma/client";
import {prismaClient} from "$lib/server/stores/prismaStore";

let prisma: PrismaClient;
prismaClient.subscribe((value) => {
    prisma = value;
});

export async function validateSessionToken(token: string): Promise<SessionValidationResult> {
    const sessionId = encodeHexLowerCase(sha256(new TextEncoder().encode(token)));
    const row = await prisma.session.findFirst({
        select: {
            id: true,
            userId: true,
            expiresAt: true,
            twoFactorVerified: true,
            user: {
                select: {
                    id: true,
                    email: true,
                    username: true,
                    emailVerified: true,
                    totpKey: true
                }
            }
        },
        where: {
            id: sessionId
        }
    });
    if (row === null) {
        return {session: null, user: null};
    }
    const session: Session = {
        id: row.id,
        userId: row.userId,
        expiresAt: row.expiresAt,
        twoFactorVerified: row.twoFactorVerified
    };
    const user: User = {
        id: row.user.id,
        email: row.user.email,
        username: row.user.username,
        emailVerified: row.user.emailVerified,
        registered2FA: row.user.totpKey !== null
    }
    if (Date.now() >= session.expiresAt.getTime()) {
        await prisma.session.delete({
            where: {
                id: session.id
            }
        });
        return {session: null, user: null};
    }
    if (Date.now() >= session.expiresAt.getTime() - 1000 * 60 * 60 * 24 * 15) {
        session.expiresAt = new Date(Date.now() + 1000 * 60 * 60 * 24 * 30);
        await prisma.session.update({
            where: {
                id: session.id
            },
            data: {
                expiresAt: session.expiresAt
            }
        });
    }
    return {session, user};
}

export async function invalidateSession(sessionId: string): Promise<void> {
    await prisma.session.delete({
        where: {
            id: sessionId
        }
    });
}

export async function invalidateUserSessions(userId: number): Promise<void> {
    await prisma.session.deleteMany({
        where: {
            userId
        }
    });
}

export function setSessionTokenCookie(event: RequestEvent, token: string, expiresAt: Date): void {
    event.cookies.set("session", token, {
        httpOnly: true,
        path: "/",
        secure: import.meta.env.PROD,
        sameSite: "lax",
        expires: expiresAt
    });
}

export function deleteSessionTokenCookie(event: RequestEvent): void {
    event.cookies.set("session", "", {
        httpOnly: true,
        path: "/",
        secure: import.meta.env.PROD,
        sameSite: "lax",
        maxAge: 0
    });
}

export function generateSessionToken(): string {
    const tokenBytes = new Uint8Array(20);
    crypto.getRandomValues(tokenBytes);
    return encodeBase32LowerCaseNoPadding(tokenBytes).toLowerCase();
}

export async function createSession(token: string, userId: number, flags: SessionFlags): Promise<Session> {
    const sessionId = encodeHexLowerCase(sha256(new TextEncoder().encode(token)));
    const session: Session = {
        id: sessionId,
        userId,
        expiresAt: new Date(Date.now() + 1000 * 60 * 60 * 24 * 30),
        twoFactorVerified: flags.twoFactorVerified
    };
    await prisma.session.create({
        data: {
            id: session.id,
            userId: session.userId,
            expiresAt: session.expiresAt,
            twoFactorVerified: session.twoFactorVerified
        }
    });
    return session;
}

export async function setSessionAs2FAVerified(sessionId: string): Promise<void> {
    await prisma.session.update({
        where: {
            id: sessionId
        },
        data: {
            twoFactorVerified: true
        }
    });
}

export interface SessionFlags {
    twoFactorVerified: boolean;
}

export interface Session extends SessionFlags {
    id: string;
    expiresAt: Date;
    userId: number;
}

type SessionValidationResult = { session: Session; user: User } | { session: null; user: null };
