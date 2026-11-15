@echo off
setlocal
chcp 65001 >nul
pushd "%~dp0"
if errorlevel 1 exit /b 1

echo.
echo  ======================================================================
echo    TP 2 - PAA  ^|  COBERTURA POR VERTICES
echo    Preparando o projeto Java 17...
echo  ======================================================================
echo.

where javac >nul 2>nul
if errorlevel 1 (
    echo  [ERRO] O compilador javac nao foi encontrado.
    echo         Instale o JDK 17 e abra o terminal novamente.
    goto :failure
)
where java >nul 2>nul
if errorlevel 1 (
    echo  [ERRO] O comando java nao foi encontrado no PATH.
    goto :failure
)

echo  [1/2] Compilando os arquivos Java...
javac -encoding UTF-8 --release 17 -d out src\*.java tests\*.java
if errorlevel 1 (
    echo.
    echo  [ERRO] A compilacao falhou. Confira as mensagens acima e o JDK 17.
    goto :failure
)

echo  [2/2] Compilacao concluida. Abrindo a aplicacao...
java -Xms128m -Xmx512m "-Dfile.encoding=UTF-8" "-Djava.awt.headless=true" -cp out Main %*
set "result=%errorlevel%"
goto :finish

:failure
set "result=1"

:finish
echo.
if "%~1"=="" (
    echo  Pressione qualquer tecla para fechar esta janela.
    pause >nul
)
popd
exit /b %result%
