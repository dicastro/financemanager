@echo off

docker run -it --rm --name bankscrapper --network=bankscrapper_bankscrapper -v /c/work/repos/financemanager/wksp/shared/dbfiles:/financemanager/dbfiles -v /c/work/repos/financemanager/wksp/shared/credentials:/financemanager/credentials -v /c/work/repos/financemanager/wksp/bankscrapper:/financemanager/bankscrapper -e SPRING_PROFILES_ACTIVE=docker qopuir/bankscrapper:1.0.0-SNAPSHOT --sync-type PAST_TWO_MONTHS --bank-credential /financemanager/credentials/kb_credentials --encryption-password /financemanager/credentials/encryption_password