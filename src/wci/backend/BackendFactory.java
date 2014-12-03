package wci.backend;

import wci.backend.compiler.CodeGenerator;

import wci.backend.interpreter.Excutor;


public class BackendFactory
{

  public static Backend createBackend(String operation)
    throws Exception
  {

    if (operation.equalsIgnoreCase("compile")) {
      return new CodeGenerator();
    }else if (operation.equalsIgnoreCase("execute")){
      return new Excutor();
    } else {
        throw new Exception("Backend Factory: Invalid OPERATION '" + operation + "'");
    }
  }

}