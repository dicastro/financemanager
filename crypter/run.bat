@echo off

docker run -it --rm --name crypter -v /c/work/repos/financemanager/wksp/shared/dbfiles:/financemanager/wksp -v /c/work/repos/financemanager/wksp/shared/credentials:/financemanager/credentials -e SPRING_PROFILES_ACTIVE=docker qopuir/crypter:1.0.0-SNAPSHOT --work-dir /financemanager/wksp --encryption-password /financemanager/credentials/encryption_password