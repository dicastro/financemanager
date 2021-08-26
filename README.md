# TODO

- PACKAGE AND DEPLOY 1.1.0

# Backlog

- Import
  - Poder subir directamente un fichero (y seleccionar el banco y la cuenta)
    - En vez de tener que dejarlos en una carpeta 'import'

- Accounts
  - Meter apartado en el que se ven los meses disponibles (con opción a borrar)
  - Meter apartado para ver el cálculo de balances de cada cuenta (fecha del último import de la cuenta y fecha del último balance calculado)
    - Con botón para calcular balances
    - Con barra de progreso en función de los ficheros a procesar
  - ¿Qué pasa con las inversiones? ¿Cómo se guardan?

- Position
  - en el detalle añadir:
    - ahorro/pérdida último mes
    - comparación ahorro/pérdida mismo mes año anterior
    - ahorro/pérdida año en curso
    - comparación ahorro/pérdida mismo periodo año anterior
  - leer las posiciones de los balances calculados
  - añadir en el history un filtro por fechas
    - con una fecha desde que como mínimo podría ser la fecha del balance inicial de la cuenta (valor por defecto)
    - y con una fecha hasta que como máximo podría ser el último mes para el que haya movimientos (valor por defecto)
    - habrá varios botones con las opciones más comunes (año en curso, año pasado, últimos 2 años, últimos 3 años, últimos 4 años, últimos 5 años)

- Movements
  - Poner boton de siguiente y anterior en el filtro año/mes

- General
  - La barra de cargando es muy sutil... debería ser una cortinilla en toda la página

- Ejecutar la imagen con otro usuario que no sea root
  - Hay que tener en cuenta los permisos de los ficheros que se creen
  - En el NAS habría que crear otro usuario y que el contenedor se ejecutase con ese mismo usuario

- Crypter
  - Que con cada comando lea la contraseña del disco (ahora mismo: para cambiar la contraseña de cifrado hace falta descifrar, SALIR, cambiar la contraseña en el fichero, volver a ejecutar crypter y finalmente cifrar de nuevo)

- Cryptoutils
  - Al descifrar que se borre el fichero meta

# Ideas

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

# HOWTO

sudo docker run --rm --name tesseract -v ~/repos/financemanager:/financemanager -it azul/zulu-openjdk-alpine:11.0.7-jre sh

apk add tesseract-ocr

tesseract /financemanager/images/keyboardScreenshot__0.png /tmp/output --tessdata-dir /financemanager/tessdata -l eng --psm 10 --oem 1 /financemanager/tessconfig/config && cat /tmp/output.txt && rm /tmp/output.txt

java -Dkbscrapper.tesseract.dataPath=/financemanager/tessdata -Dkbscrapper.tesseract.configPath=/financemanager/tessconfig/config -Dkbscrapper.keyboardCache.basePath=/financemanager/keyboards/kb -Dkbscrapper.images.basePath=/financemanager/images -jar /financemanager/kbscrapper/target/kbscrapper-0.0.1-SNAPSHOT.jar keyboard01.png

# Links

- [Spring Boot application events explained](https://reflectoring.io/spring-boot-application-events-explained/)
- [String events](https://www.baeldung.com/spring-events)
- [Text To ASCII](https://patorjk.com/software/taag/#p=display&f=Big&t=Finance%20Manager%20App)
- [Prevent Brute Force Authentication Attempts with Spring Security](https://www.baeldung.com/spring-security-block-brute-force-authentication-attempts)

# NOTES

### Vaadin - Custom Components used

- [MultiSelectComboBox](https://vaadin.com/directory/component/multiselect-combo-box)
  - There is no component supported by Vaadin
  - In [this](https://github.com/vaadin/web-components/issues/1388) issue it has been proposed, and someone has implemented the above version
  - This component has been several years in the Vaadin roadmap, but it has been always deprioritized
- [ApexCharts](https://vaadin.com/directory/component/apexchartsjs)
