# TODO

- Position
  - la primera vez que se muestra la grafica no ocupa todo el espacio, si se oculta y se vuelve a mostrar sí
  - añadir en el history un filtro por fechas
    - con una fecha desde que como mínimo podría ser la fecha del balance inicial de la cuenta (valor por defecto)
    - y con una fecha hasta que como máximo podría ser el último mes para el que haya movimientos (valor por defecto)
    - habrá varios botones con las opciones más comunes (año en curso, año pasado, últimos 2 años, últimos 3 años, últimos 4 años, últimos 5 años)

- General
  - Revisar altura de las pantallas (en Imports se hace bien... y en esta no se añade ningún layout adicional)
  - Al hacer logout forzar la limpieza de la caché

- Icono para la aplicación

- PACKAGE AND DEPLOY 1.1.0

# Backlog

- Import
  - Poder subir directamente un fichero (y seleccionar el banco y la cuenta)
    - En vez de tener que dejarlos en una carpeta 'import'

- General
  - La barra de cargando es muy sutil... debería ser una cortinilla en toda la página

- Ejecutar la imagen con otro usuario que no sea root
  - Hay que tener en cuenta los permisos de los ficheros que se creen
  - En el NAS habría que crear otro usuario y que el contenedor se ejecutase con ese mismo usuario

- Añadir sección para estudiar el consumo eléctrico
  - se podrán cargar los ficheros csv de consumo por horas
  - se podrán definir los horarios y tarifas
  - se podrá hacer una comparativa con otras tarifas

- Añadir sección para estudiar la hipoteca
  - se introduce el capital pendiente
  - se introduce el tipo de interés (fijo descontando bonificaciones)
  - calcula las cuotas y la parte amortizada cada mes y la parte de intereses
  - calcula lo que queda pendiente de pago de intereses
  - calcula el ahorro comparando con otro tipo de interés
  - calcula el ahorro amortizando anticipadamente

- Crypter
  - Que con cada comando lea la contraseña del disco (ahora mismo: para cambiar la contraseña de cifrado hace falta descifrar, SALIR, cambiar la contraseña en el fichero, volver a ejecutar crypter y finalmente cifrar de nuevo)

- Cryptoutils
  - Al descifrar que se borre el fichero meta

# Ideas

- Mover la gestión de cuentas (Account) a la sección 'Administration' ??

# HOWTO

sudo docker run --rm --name tesseract -v ~/repos/financemanager:/financemanager -it azul/zulu-openjdk-alpine:11.0.7-jre sh

apk add tesseract-ocr

tesseract /financemanager/images/keyboardScreenshot__0.png /tmp/output --tessdata-dir /financemanager/tessdata -l eng --psm 10 --oem 1 /financemanager/tessconfig/config && cat /tmp/output.txt && rm /tmp/output.txt

java -Dkbscrapper.tesseract.dataPath=/financemanager/tessdata -Dkbscrapper.tesseract.configPath=/financemanager/tessconfig/config -Dkbscrapper.keyboardCache.basePath=/financemanager/keyboards/kb -Dkbscrapper.images.basePath=/financemanager/images -jar /financemanager/kbscrapper/target/kbscrapper-0.0.1-SNAPSHOT.jar keyboard01.png

# Links

- [Spring Boot application events explained](https://reflectoring.io/spring-boot-application-events-explained/)
- [String events](https://www.baeldung.com/spring-events)
- [Text To ASCII](https://patorjk.com/software/taag/#p=display&f=Big&t=Finance%20Manager%20App)

# NOTES

### Vaadin - Custom Components used

- [MultiSelectComboBox](https://vaadin.com/directory/component/multiselect-combo-box)
  - There is no component supported by Vaadin
  - In [this](https://github.com/vaadin/web-components/issues/1388) issue it has been proposed, and someone has implemented the above version
  - This component has been several years in the Vaadin roadmap, but it has been always deprioritized
- [ApexCharts](https://vaadin.com/directory/component/apexchartsjs)
