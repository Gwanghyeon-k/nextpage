CREATE TABLE "users" (
    "id" SERIAL PRIMARY KEY,
    "email" VARCHAR(255) UNIQUE NOT NULL,
    "nickname" VARCHAR(255) UNIQUE NOT NULL,
    "createdAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP,
    "isDeleted" BOOLEAN DEFAULT FALSE
    );

CREATE TABLE "bookmarks" (
    "id" SERIAL PRIMARY KEY,
    "userId" INT NOT NULL REFERENCES "users"("id") ON DELETE CASCADE,
    "storyId" INT NOT NULL,
    "imageUrl" VARCHAR(255) NOT NULL,
    "createdAt" TIMESTAMP NOT NULL,
    "isDeleted" BOOLEAN NOT NULL
    );

INSERT INTO "users" ("email", "nickname", "createdAt", "isDeleted")
VALUES ('user1@example.com', 'user1', CURRENT_TIMESTAMP, FALSE),
       ('user2@example.com', 'user2', CURRENT_TIMESTAMP, FALSE);

INSERT INTO "bookmarks" ("userId", "storyId", "imageUrl", "createdAt", "isDeleted")
VALUES (1, 1, 'https://www.sisain.co.kr/news/photo/202303/49959_91104_5850.jpg', CURRENT_TIMESTAMP, FALSE),
       (1, 2, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQztO1tf_NSVOHOqIy-Ztuku-ChOwUxOUfA6Q&s', CURRENT_TIMESTAMP, FALSE),
       (2, 3, 'https://cdn.pixabay.com/photo/2023/11/21/12/42/ai-generated-8403309_1280.png', CURRENT_TIMESTAMP, FALSE);
