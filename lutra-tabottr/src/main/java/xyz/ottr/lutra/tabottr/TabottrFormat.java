package xyz.ottr.lutra.tabottr;

import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.io.TemplateWriter;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.tabottr.parser.ExcelReader;

public class TabottrFormat implements Format {
    
    private final InstanceReader instanceReader;
    
    public TabottrFormat() {
        this.instanceReader = new InstanceReader(new ExcelReader());
    }

    @Override
    public Result<InstanceReader> getInstanceReader() {
        return Result.of(this.instanceReader);
    }

    @Override
    public boolean supports(Operation op, ObjectType ot) {
       return op == Operation.read && ot == ObjectType.instance; 
    }

    @Override
    public String getDefaultFileSuffix() {
        return ".xlsx";
    }

    @Override
    public String getFormatName() {
        return "TabOTTR";
    }
}
