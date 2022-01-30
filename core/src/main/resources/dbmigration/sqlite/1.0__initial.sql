-- apply changes
create table players (
  id                            integer not null,
  uuid                          varchar(40) not null,
  verified                      int default 0 not null,
  discord_id                    varchar(255) not null,
  verification_message_id       varchar(255),
  constraint uq_players_uuid unique (uuid),
  constraint uq_players_discord_id unique (discord_id),
  constraint pk_players primary key (id)
);

