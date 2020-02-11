package xyz.ottr.lutra.bottr;

import xyz.ottr.lutra.bottr.io.BInstanceReader;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.result.Result;

public class BottrFormat implements Format {
    
    private final InstanceReader instanceReader;
    
    public BottrFormat() {
        this.instanceReader = new InstanceReader(new BInstanceReader());
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
        return ".ttl"; // TODO: Is this correct?
    }

    @Override
    public String getFormatName() {
        return "BOTTR";
    }
}
