package xyz.ottr.lutra.wottr;

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.io.TemplateWriter;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.io.RDFFileReader;
import xyz.ottr.lutra.wottr.parser.v04.WInstanceParser;
import xyz.ottr.lutra.wottr.parser.v04.WTemplateParser;
import xyz.ottr.lutra.wottr.writer.v04.WInstanceWriter;
import xyz.ottr.lutra.wottr.writer.v04.WTemplateWriter;

public class WottrFormat implements Format {

    private final TemplateReader templateReader;
    private final TemplateWriter templateWriter;
    private final InstanceReader instanceReader;
    private final InstanceWriter instanceWriter;
    
    public WottrFormat(PrefixMapping prefixes) {
        this.templateReader = new TemplateReader(new RDFFileReader(), new WTemplateParser());
        this.templateWriter = new WTemplateWriter(prefixes);
        this.instanceReader = new InstanceReader(new RDFFileReader(), new WInstanceParser());
        this.instanceWriter = new WInstanceWriter(prefixes);
    }

    @Override
    public Result<TemplateReader> getTemplateReader() {
        return Result.of(this.templateReader);
    }

    @Override
    public Result<TemplateWriter> getTemplateWriter() {
        return Result.of(this.templateWriter);
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
        return true;
    }

    @Override
    public String getDefaultFileSuffix() {
        return ".ttl";
    }

    @Override
    public String getFormatName() {
        return "WOTTR";
    }
}
