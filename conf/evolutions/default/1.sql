# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table linked_account (
  id                        bigint not null,
  user_id                   bigint,
  provider_user_id          varchar(255),
  provider_key              varchar(255),
  constraint pk_linked_account primary key (id))
;

create table security_role (
  id                        bigint not null,
  role_name                 varchar(255),
  constraint pk_security_role primary key (id))
;

create table token_action (
  id                        bigint not null,
  token                     varchar(255),
  target_user_id            bigint,
  type                      varchar(2),
  created                   timestamp,
  expires                   timestamp,
  constraint ck_token_action_type check (type in ('PR','EV')),
  constraint uq_token_action_token unique (token),
  constraint pk_token_action primary key (id))
;

create table users (
  id                        bigint not null,
  email                     varchar(255),
  name                      varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  last_login                timestamp,
  active                    boolean,
  email_validated           boolean,
  constraint pk_users primary key (id))
;

create table user_permission (
  id                        bigint not null,
  value                     varchar(255),
  constraint pk_user_permission primary key (id))
;


create table users_security_role (
  users_id                       bigint not null,
  security_role_id               bigint not null,
  constraint pk_users_security_role primary key (users_id, security_role_id))
;

create table users_user_permission (
  users_id                       bigint not null,
  user_permission_id             bigint not null,
  constraint pk_users_user_permission primary key (users_id, user_permission_id))
;
create sequence linked_account_seq;

create sequence security_role_seq;

create sequence token_action_seq;

create sequence users_seq;

create sequence user_permission_seq;

alter table linked_account add constraint fk_linked_account_user_1 foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_linked_account_user_1 on linked_account (user_id);
alter table token_action add constraint fk_token_action_targetUser_2 foreign key (target_user_id) references users (id) on delete restrict on update restrict;
create index ix_token_action_targetUser_2 on token_action (target_user_id);



alter table users_security_role add constraint fk_users_security_role_users_01 foreign key (users_id) references users (id) on delete restrict on update restrict;

alter table users_security_role add constraint fk_users_security_role_securi_02 foreign key (security_role_id) references security_role (id) on delete restrict on update restrict;

alter table users_user_permission add constraint fk_users_user_permission_user_01 foreign key (users_id) references users (id) on delete restrict on update restrict;

alter table users_user_permission add constraint fk_users_user_permission_user_02 foreign key (user_permission_id) references user_permission (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists linked_account;

drop table if exists security_role;

drop table if exists token_action;

drop table if exists users;

drop table if exists users_security_role;

drop table if exists users_user_permission;

drop table if exists user_permission;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists linked_account_seq;

drop sequence if exists security_role_seq;

drop sequence if exists token_action_seq;

drop sequence if exists users_seq;

drop sequence if exists user_permission_seq;

