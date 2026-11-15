@echo off
setlocal
chcp 65001 >nul
pushd "%~dp0"
if errorlevel 1 exit /b 1
if not exist build mkdir build
echo.
echo  ================================================================
echo    RELATORIO SBC - COBERTURA POR VERTICES
echo  ================================================================
where pdflatex >nul 2>nul
if errorlevel 1 (
    echo  [ERRO] Instale uma distribuicao LaTeX ou importe o ZIP no Overleaf.
    goto :failed
)
where bibtex >nul 2>nul
if errorlevel 1 (
    echo  [ERRO] BibTeX nao encontrado no PATH.
    goto :failed
)
echo  [1/5] Primeira passagem do pdfLaTeX...
pdflatex -interaction=nonstopmode -halt-on-error -file-line-error main.tex >build/latex-1.txt 2>&1
if errorlevel 1 goto :failed
echo  [2/5] Preparando a bibliografia...
bibtex main >build/bibtex.txt 2>&1
if errorlevel 1 goto :failed
for %%P in (2 3 4) do (
    echo  [pdfLaTeX] Resolucao de referencias - passagem %%P...
    pdflatex -interaction=nonstopmode -halt-on-error -file-line-error main.tex >build/latex-%%P.txt 2>&1
    if errorlevel 1 goto :failed
)
echo.
echo  [OK] PDF gerado: %CD%\main.pdf
echo  Logs locais: %CD%\build
set "result=0"
goto :finish
:failed
echo.
echo  [ERRO] A compilacao falhou. Consulte main.log e os logs em build.
set "result=1"
:finish
if "%~1"=="" pause
popd
exit /b %result%
