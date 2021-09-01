# TODO

- Import
  - Borrar el viejo import y todo lo que no se use

- Accounts
  - Poder borrar una cuenta
    - Al borrar una cuenta también se borran todos sus movimientos e inversiones

- Administration
  - Al tener botón de descargar/cargar backup y la carga directa de ficheros ya no hace falta los volúmenes que mapean a una carpeta de disco
    - Se pueden utilizar volúmenes docker
      - Al usar los volúmenes docker, se puede ejecutar el contenedor con un usuario que no sea root

- PACKAGE and DEPLOY 1.2.0

# Backlog

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

- PlannedBudgets
  - Incluir diálogo de confirmación al borrar un elemento

- PlannedExpenses
  - Incluir diálogo de confirmación al borrar un elemento

- Crear librería de iconos para todos los iconos de financemanager (logos de bancos y propósitos de cuentas)
  - Usar [IcoMoon](https://icomoon.io/)
  - [Aquí preguntan sobre ello](https://vaadin.com/forum/thread/18364785/how-to-include-custom-icon-sets-with-vaadin-14-3-0)
  - Esto solucionaría el problema de las columnas de tablas con las imágenes que se cortan en dispositivos móviles
  - https://vaadin.com/docs/v8/framework/articles/UsingFontIcons
  - https://vaadin.com/docs/v8/framework/themes/themes-fonticon

- Externalizar los importers
  - Como si fueran una especie de plugin de tal forma que la aplicación sea más reutilizable
    si alguien quiere utilizarla y tiene otros bancos o exporta los datos de otra forma, tendría que
    crearse un jar con sus importers y el resto funcionaría

- General
  - La barra de cargando es muy sutil y no se ve
    - Crear componente que sea un diálogo con una barrita de cargando y que tenga la posibilidad de mostrar textos con lo que se va haciendo
    - Ej del proceso de carga de un backup:
      - Renombrando ficheros
      - Descomprimiendo backup en directorio temporal
      - Copiando backup
      - Borrando ficheros originales
      - Limpiando directorio temporal
    - Incluir el componente de carga en:
      - Carga de backup

- Al hacer el backup incluir un fichero generado con metadatos del backup
  - Que incluya: fecha de generación, número de ficheros incluidos, nombres de ficheros incluidos, hashes de ficheros incluidos
  - Al cargar un backup se comprobará con el fichero de metadatos si los datos cargados con correctos

- Gestionar errores en Eventos Spring
  - Hay un evento 'AccountDeletedEvent' que borra movements e investments después de borrar una cuenta
    - ¿Qué pasa si falla ese borrado? Se quedarían movements o investments ocupando espacio
        - Añadir algún mecanismo de limpieza que borre estos ficheros sin cuenta asociada cada X tiempo

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
