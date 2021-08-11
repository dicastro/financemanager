@echo off

docker run -it --rm --name financemanagerapp -v /c/work/repos/financemanager/wksp:/financemanager/wksp -e SPRING_PROFILES_ACTIVE=docker qopuir/financemanagerapp:1.0.0-SNAPSHOT