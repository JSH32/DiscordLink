-- apply changes
create table players (
  id                            integer not null,
  uuid                          varchar(40) not null,
  verified                      int default 0 not null,
  discord_id                    varchar(255) not null,
  constraint uq_players_uuid unique (uuid),
  constraint uq_players_discord_id unique (discord_id),
  constraint pk_players primary key (id)
);

create table verifications (
  id                            integer not null,
  player_id                     integer not null,
  type                          varchar(16),
  value                         varchar(255),
  constraint ck_verifications_type check ( type in ('message_reaction','code')),
  constraint uq_verifications_player_id unique (player_id),
  constraint uq_verifications_value unique (value),
  constraint pk_verifications primary key (id),
  foreign key (player_id) references players (id) on delete cascade on update restrict
);

