import type {PrismaClient} from "@prisma/client";
import {prismaClient} from "$lib/server/stores/prismaStore";

let prisma: PrismaClient;
prismaClient.subscribe((value) => {
    prisma = value;
});

export function verifyEmailInput(email: string): boolean {
    return /^.+@.+\..+$/.test(email) && email.length < 256;
}

export async function checkEmailAvailability(email: string): Promise<boolean> {
    const row = await prisma.user.count({
        where: {
            email
        }
    });
    if (row === null) {
        throw new Error();
    }
    return row === 0;
}
