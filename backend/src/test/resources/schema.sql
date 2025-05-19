-- Schema for testing

-- Drop tables if they exist to avoid conflicts
DROP TABLE IF EXISTS "task_tags";
DROP TABLE IF EXISTS "user_roles";
DROP TABLE IF EXISTS "notifications";
DROP TABLE IF EXISTS "tasks";
DROP TABLE IF EXISTS "team_members";
DROP TABLE IF EXISTS "teams";
DROP TABLE IF EXISTS "users";
DROP TABLE IF EXISTS "roles";
DROP TABLE IF EXISTS "tags";

-- Create tables in proper order (base tables first, then tables with foreign keys)

-- Create roles table
CREATE TABLE "roles" (
    "id" UUID PRIMARY KEY,
    "name" VARCHAR(50) NOT NULL,
    "description" VARCHAR(500),
    "createdAt" TIMESTAMP NOT NULL,
    "updatedAt" TIMESTAMP NOT NULL,
    "version" BIGINT
);

-- Create tags table
CREATE TABLE "tags" (
    "id" UUID PRIMARY KEY,
    "name" VARCHAR(50) NOT NULL,
    "color" VARCHAR(7),
    "createdAt" TIMESTAMP NOT NULL,
    "updatedAt" TIMESTAMP NOT NULL,
    "version" BIGINT
);

-- Create users table
CREATE TABLE "users" (
    "id" UUID PRIMARY KEY,
    "firstName" VARCHAR(50) NOT NULL,
    "lastName" VARCHAR(50) NOT NULL,
    "email" VARCHAR(255) NOT NULL UNIQUE,
    "password" VARCHAR(255) NOT NULL,
    "createdAt" TIMESTAMP NOT NULL,
    "updatedAt" TIMESTAMP NOT NULL,
    "version" BIGINT
);

-- Create teams table
CREATE TABLE "teams" (
    "id" UUID PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "description" VARCHAR(255),
    "teamLeadId" UUID NOT NULL,
    "createdAt" TIMESTAMP NOT NULL,
    "updatedAt" TIMESTAMP NOT NULL,
    "version" BIGINT,
    FOREIGN KEY ("teamLeadId") REFERENCES "users"("id")
);

-- Create tasks table
CREATE TABLE "tasks" (
    "id" UUID PRIMARY KEY,
    "title" VARCHAR(100) NOT NULL,
    "shortDescription" VARCHAR(200) NOT NULL,
    "longDescription" TEXT,
    "dueDate" TIMESTAMP NOT NULL,
    "priority" VARCHAR(20) NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "assigneeId" UUID,
    "teamId" UUID,
    "createdById" UUID NOT NULL,
    "createdAt" TIMESTAMP NOT NULL,
    "updatedAt" TIMESTAMP NOT NULL,
    "version" BIGINT,
    FOREIGN KEY ("assigneeId") REFERENCES "users"("id"),
    FOREIGN KEY ("teamId") REFERENCES "teams"("id"),
    FOREIGN KEY ("createdById") REFERENCES "users"("id")
);

-- Create notifications table
CREATE TABLE "notifications" (
    "id" UUID PRIMARY KEY,
    "message" VARCHAR(255) NOT NULL,
    "read" BOOLEAN NOT NULL DEFAULT FALSE,
    "userId" UUID NOT NULL,
    "createdAt" TIMESTAMP NOT NULL,
    "updatedAt" TIMESTAMP NOT NULL,
    "version" BIGINT,
    FOREIGN KEY ("userId") REFERENCES "users"("id")
);

-- Create join tables for many-to-many relationships
CREATE TABLE "user_roles" (
    "userId" UUID NOT NULL,
    "roleId" UUID NOT NULL,
    PRIMARY KEY ("userId", "roleId"),
    FOREIGN KEY ("userId") REFERENCES "users"("id"),
    FOREIGN KEY ("roleId") REFERENCES "roles"("id")
);

CREATE TABLE "task_tags" (
    "taskId" UUID NOT NULL,
    "tagId" UUID NOT NULL,
    PRIMARY KEY ("taskId", "tagId"),
    FOREIGN KEY ("taskId") REFERENCES "tasks"("id"),
    FOREIGN KEY ("tagId") REFERENCES "tags"("id")
);

-- Create team members join table
CREATE TABLE "team_members" (
    "teamId" UUID NOT NULL,
    "userId" UUID NOT NULL,
    PRIMARY KEY ("teamId", "userId"),
    FOREIGN KEY ("teamId") REFERENCES "teams"("id"),
    FOREIGN KEY ("userId") REFERENCES "users"("id")
);
