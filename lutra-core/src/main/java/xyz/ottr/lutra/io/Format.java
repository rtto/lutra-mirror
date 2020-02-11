package xyz.ottr.lutra.io;

import xyz.ottr.lutra.result.Result;

public interface Format {
    
    public enum Operation { read, write }
    public enum ObjectType { template, instance }
    
    default Result<TemplateReader> getTemplateReader() {
        return Result.error("Reading templates not supported for format " + getFormatName());
    }

    default Result<TemplateWriter> getTemplateWriter() {
        return Result.error("Writing templates not supported for format " + getFormatName());
    }

    default Result<InstanceReader> getInstanceReader() {
        return Result.error("Reading instances not supported for format " + getFormatName());
    }

    default Result<InstanceWriter> getInstanceWriter() {
        return Result.error("Writing instances not supported for format " + getFormatName());
    }
    
    boolean supports(Operation op, ObjectType ot);
    
    String getDefaultFileSuffix();
    
    String getFormatName();
    
    default boolean supportsTemplateWriter() {
        return supports(Operation.write, ObjectType.template);
    }

    default boolean supportsTemplateReader() {
        return supports(Operation.read, ObjectType.template);
    }

    default boolean supportsInstanceWriter() {
        return supports(Operation.write, ObjectType.instance);
    }

    default boolean supportsInstanceReader() {
        return supports(Operation.read, ObjectType.instance);
    }
}
