#!/bin/sh

psql -U postgres -d template1 -c "create extension adminpack;create extension hstore;"
psql -U postgres -d postgres -c "create user bigdata with nosuperuser replication encrypted password 'Bigdata.2020';"
psql -U postgres -d postgres -c "create database bigdata owner = bigdata template = template1;"

psql -U bigdata -d bigdata -f /data/init.sql
#psql -U bigdata -d bigdata -f /data/workflow.sql
#psql -U bigdata -d bigdata -f /data/job.sql
