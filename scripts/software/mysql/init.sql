set names 'utf8mb4';
create database bigdata character set = 'utf8mb4';
grant select on mysql.* to 'bigdata'@'%';

use bigdata;
-- init tables, views, sequences  begin
create table name_test
(
    id   bigint auto_increment,
    name varchar(255),
    t    timestamp,
    seq  int,
    primary key (id)
);
-- init tables, views, sequences  end

grant all on bigdata.* to 'bigdata'@'%';
