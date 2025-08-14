sudo docker exec -i pgsql-sherbinki3 psql -U alex -d postgres -c "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE datname='mydb';"
sudo docker exec -i pgsql-sherbinki3 psql -U alex -d postgres -c "DROP DATABASE mydb;"
sudo docker exec -i pgsql-sherbinki3 psql -U alex -d postgres -c "CREATE DATABASE mydb OWNER alex;"