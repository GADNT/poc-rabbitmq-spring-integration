title rs
set APP_ARGS=-Xms256m -Xmx256m -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8900


call gradle clean build -x test

FOR /F "tokens=* USEBACKQ" %%F IN (`dir /b build\libs\*.jar`) DO (
  SET JAR_FILE=%%F
)

ECHO %JAR_FILE%

call java %APP_ARGS% -jar build\libs\%JAR_FILE%

pause