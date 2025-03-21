import {encodeHexLowerCase} from "@oslojs/encoding";
import {generateRandomOTP} from "./utils";
import {sha256} from "@oslojs/crypto/sha2";

import type {RequestEvent} from "@sveltejs/kit";
import type {User} from "./user";
import type {PrismaClient} from "@prisma/client";
import {prismaClient} from "$lib/server/stores/prismaStore";

let prisma: PrismaClient;
prismaClient.subscribe((value) => {
    prisma = value;
});

export async function createPasswordResetSession(token: string, userId: number, email: string): Promise<PasswordResetSession> {
    const sessionId = encodeHexLowerCase(sha256(new TextEncoder().encode(token)));
    const session: PasswordResetSession = {
        id: sessionId,
        userId,
        email,
        expiresAt: new Date(Date.now() + 1000 * 60 * 10),
        code: generateRandomOTP(),
        emailVerified: false,
        twoFactorVerified: false
    };
    await prisma.passwordResetSession.create({
        data: {
            id: session.id,
            userId: session.userId,
            email: session.email,
            code: session.code,
            expiresAt: session.expiresAt
        }
    });
    return session;
}

export async function validatePasswordResetSessionToken(token: string): Promise<PasswordResetSessionValidationResult> {
    const sessionId = encodeHexLowerCase(sha256(new TextEncoder().encode(token)));
    const row = await prisma.passwordResetSession.findUnique({
        where: {
            id: sessionId
        },
        select: {
            id: true,
            userId: true,
            email: true,
            code: true,
            expiresAt: true,
            emailVerified: true,
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
        }
    });
    if (row === null) {
        return {session: null, user: null};
    }
    const session: PasswordResetSession = {
        id: row.id,
        userId: row.userId,
        email: row.email,
        code: row.code,
        expiresAt: row.expiresAt,
        emailVerified: row.emailVerified,
        twoFactorVerified: row.twoFactorVerified
    }
    const user: User = {
        id: row.user.id,
        email: row.user.email,
        username: row.user.username,
        emailVerified: row.user.emailVerified,
        registered2FA: row.user.totpKey !== null
    }
    if (Date.now() >= session.expiresAt.getTime()) {
        await prisma.passwordResetSession.delete({
            where: {
                id: session.id
            }
        });
        return {session: null, user: null};
    }
    return {session, user};
}

export async function setPasswordResetSessionAsEmailVerified(sessionId: string): Promise<void> {
    await prisma.passwordResetSession.update({
        where: {
            id: sessionId
        },
        data: {
            emailVerified: true
        }
    });
}

export async function setPasswordResetSessionAs2FAVerified(sessionId: string): Promise<void> {
    await prisma.passwordResetSession.update({
        where: {
            id: sessionId
        },
        data: {
            twoFactorVerified: true
        }
    });
}

export async function invalidateUserPasswordResetSessions(userId: number): Promise<void> {
    await prisma.passwordResetSession.deleteMany({
        where: {
            userId
        }
    });
}

export async function validatePasswordResetSessionRequest(event: RequestEvent): Promise<PasswordResetSessionValidationResult> {
    const token = event.cookies.get("password_reset_session") ?? null;
    if (token === null) {
        return {session: null, user: null};
    }
    const result = await validatePasswordResetSessionToken(token);
    if (result.session === null) {
        deletePasswordResetSessionTokenCookie(event);
    }
    return result;
}

export function setPasswordResetSessionTokenCookie(event: RequestEvent, token: string, expiresAt: Date): void {
    event.cookies.set("password_reset_session", token, {
        expires: expiresAt,
        sameSite: "lax",
        httpOnly: true,
        path: "/",
        secure: !import.meta.env.DEV
    });
}

export function deletePasswordResetSessionTokenCookie(event: RequestEvent): void {
    event.cookies.set("password_reset_session", "", {
        maxAge: 0,
        sameSite: "lax",
        httpOnly: true,
        path: "/",
        secure: !import.meta.env.DEV
    });
}

export function sendPasswordResetEmail(email: string, code: string): void {
    console.log(`To ${email}: Your reset code is ${code}`);
}

export interface PasswordResetSession {
    id: string;
    userId: number;
    email: string;
    expiresAt: Date;
    code: string;
    emailVerified: boolean;
    twoFactorVerified: boolean;
}

export type PasswordResetSessionValidationResult =
    | { session: PasswordResetSession; user: User }
    | { session: null; user: null };
