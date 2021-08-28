@echo off

docker run -it --rm --name financemanagerapp -p 8080:8080 -v /c/work/repos/financemanager/wksp/import:/financemanager/import -v /c/work/repos/financemanager/wksp/shared/dbfiles:/financemanager/dbfiles -e SPRING_PROFILES_ACTIVE=docker -e FINANCEMANAGER_USERS="user:{noop}1@USER|admin:{noop}1@USER,ADMIN" qopuir/financemanagerapp:1.2.0