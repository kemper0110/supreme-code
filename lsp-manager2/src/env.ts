import {z} from "zod";

export const EnvSchema = z.object({
    PORT: z.coerce.number().default(3000),
    NAMESPACE: z.string().default('default'),
});

export const env = EnvSchema.parse(process.env);