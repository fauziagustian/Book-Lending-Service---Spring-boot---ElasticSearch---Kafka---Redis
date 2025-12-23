--liquibase formatted sql
--changeset system:001-init

CREATE TABLE books (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(255) NOT NULL,
  isbn VARCHAR(32) NOT NULL,
  total_copies INT NOT NULL CHECK (total_copies >= 0),
  available_copies INT NOT NULL CHECK (available_copies >= 0),
  version BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE books
  ADD CONSTRAINT uk_books_isbn UNIQUE (isbn);

CREATE TABLE members (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL
);

ALTER TABLE members
  ADD CONSTRAINT uk_members_email UNIQUE (email);

CREATE TABLE loans (
  id BIGSERIAL PRIMARY KEY,
  book_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  borrowed_at TIMESTAMPTZ NOT NULL,
  due_date TIMESTAMPTZ NOT NULL,
  returned_at TIMESTAMPTZ NULL,

  CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books(id),
  CONSTRAINT fk_loans_member FOREIGN KEY (member_id) REFERENCES members(id)
);

CREATE INDEX idx_loans_member_active ON loans(member_id, returned_at);
CREATE INDEX idx_loans_due_active ON loans(member_id, due_date, returned_at);
