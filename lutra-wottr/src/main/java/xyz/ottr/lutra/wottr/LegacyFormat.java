package xyz.ottr.lutra.wottr;

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.io.TemplateWriter;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.io.RDFFileReader;
import xyz.ottr.lutra.wottr.parser.v03.WInstanceParser;
import xyz.ottr.lutra.wottr.parser.v03.WTemplateParser;
import xyz.ottr.lutra.wottr.writer.v03.WInstanceWriter;

public class LegacyFormat implements Format {

    private final TemplateReader templateReader;
    private final InstanceReader instanceReader;
    private final InstanceWriter instanceWriter;
    
    public LegacyFormat() {
        this.templateReader = new TemplateReader(new RDFFileReader(), new WTemplateParser());
        this.instanceReader = new InstanceReader(new RDFFileReader(), new WInstanceParser());
        this.instanceWriter = new WInstanceWriter();
    }

    @Override
    public Result<TemplateReader> getTemplateReader() {
        return Result.of(this.templateReader);
    }

    @Override
    public Result<InstanceReader> getInstanceReader() {
        return Result.of(this.instanceReader);
    }

    @Override
    public Result<InstanceWriter> getInstanceWriter() {
        return Result.of(this.instanceWriter);
    }

    @Override
    public boolean supports(Format.Operation op, Format.ObjectType ot) {
        return op == Format.Operation.read || ot == Format.ObjectType.instance;
    }
    
    @Override
    public String getDefaultFileSuffix() {
        return ".ttl";
    }

    @Override
    public String getFormatName() {
        return "legacy";
    }
}
