--liquibase formatted sql

--changeset yunyaev-vs:tables
create table repositories (
    id int not null primary key,
    github_id bigint,
    name varchar(255) not null unique,
    description varchar,
    html_url varchar(255)
);
create table commit_comments (
    id int not null primary key,
    repo_id int REFERENCES repositories (id),
    body varchar,
    github_id bigint,
    http_url varchar(255),
    user_login varchar(64)
);
--rollback drop table commit_comments;
--rollback drop table repositories;
;

--changeset yunyaev-vs:sequences
create sequence s_repositories_id;
create sequence s_commit_comments_id;
--rollback drop sequence s_commit_comments_id;
--rollback drop sequence s_repositories_id;
;


