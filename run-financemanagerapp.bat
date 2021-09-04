@echo off

docker run -it --rm --name financemanagerapp -p 8080:8080 -v financemanagerapp-dbfiles:/financemanager/dbfiles -e SPRING_PROFILES_ACTIVE=localdocker -e FINANCEMANAGER_USERS="user:{noop}1@USER|admin:{noop}1@USER,ADMIN" qopuir/financemanagerapp:1.2.0