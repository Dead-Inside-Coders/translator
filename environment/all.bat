@echo off
cls
TASM.EXE -L -ZI %1.asm
IF errorlevel 1 goto err_end
rem pause

TLINK.EXE -M -V %1.obj
IF errorlevel 1 goto err_end
rem pause
%1.exe
goto end

:err_end
pause
:end
@echo on