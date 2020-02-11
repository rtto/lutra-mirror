package xyz.ottr.lutra.stottr;

import java.util.Map;

import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.io.TemplateWriter;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.stottr.io.SFileReader;
import xyz.ottr.lutra.stottr.parser.SInstanceParser;
import xyz.ottr.lutra.stottr.parser.STemplateParser;
import xyz.ottr.lutra.stottr.writer.SInstanceWriter;
import xyz.ottr.lutra.stottr.writer.STemplateWriter;

public class StottrFormat implements Format {
    
    private final TemplateReader templateReader;
    private final TemplateWriter templateWriter;
    private final InstanceReader instanceReader;
    private final InstanceWriter instanceWriter;
    
    public StottrFormat(Map<String, String> prefixes) {
        this.templateReader = new TemplateReader(new SFileReader(), new STemplateParser());
        this.templateWriter = new STemplateWriter(prefixes);
        this.instanceReader = new InstanceReader(new SFileReader(), new SInstanceParser());
        this.instanceWriter = new SInstanceWriter(prefixes);
    }

    @Override
    public Result<TemplateReader> getTemplateReader() {
        return Result.of(this.templateReader);
    }

    @Override
    public Result<TemplateWriter> getTemplateWriter() {
        // TODO Auto-generated method stub
        return Result.of(this.templateWriter);
    }

    @Override
    public Result<InstanceReader> getInstanceReader() {
        return Result.of(this.instanceReader);
    }

    @Override
    public Result<InstanceWriter> getInstanceWriter() {
        // TODO Auto-generated method stub
        return Result.of(this.instanceWriter);
    }

    @Override
    public boolean supports(Operation op, ObjectType ot) {
        return true;
    }

    @Override
    public String getDefaultFileSuffix() {
        return ".stottr";
    }

    @Override
    public String getFormatName() {
        return "STOTTR";
    }

}
