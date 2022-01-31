-- apply changes
create table players (
  id                            bigint auto_increment not null,
  uuid                          varchar(40) not null comment 'Mojang assigned UUID',
  verified                      tinyint(1) default 0 not null comment 'Whether the user has verified their discord account linkage',
  discord_id                    varchar(255) not null comment 'Discord snowflake ID',
  constraint uq_players_uuid unique (uuid),
  constraint uq_players_discord_id unique (discord_id),
  constraint pk_players primary key (id)
);

create table verifications (
  id                            bigint auto_increment not null,
  player_id                     bigint not null,
  type                          varchar(16) comment 'Type of verification',
  value                         varchar(255) comment 'Value of the verification, this is a messageId in message_reaction and a code in code',
  constraint uq_verifications_player_id unique (player_id),
  constraint uq_verifications_value unique (value),
  constraint pk_verifications primary key (id)
);

alter table verifications add constraint fk_verifications_player_id foreign key (player_id) references players (id) on delete cascade on update restrict;

