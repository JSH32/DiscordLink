-- apply changes
create table players (
  id                            bigint auto_increment not null,
  uuid                          varchar(40) not null comment 'Mojang assigned UUID',
  verified                      tinyint(1) default 0 not null comment 'Weather the user has verified their discord account linkage',
  discord_id                    varchar(255) not null comment 'Discord snowflake ID',
  verification_message_id       varchar(255) comment 'ID of the discord message sent during verification',
  constraint uq_players_uuid unique (uuid),
  constraint uq_players_discord_id unique (discord_id),
  constraint pk_players primary key (id)
);

