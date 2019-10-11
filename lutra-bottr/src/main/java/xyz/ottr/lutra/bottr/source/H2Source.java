package xyz.ottr.lutra.bottr.source;

/*-
 * #%L
 * lutra-bottr
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.nio.file.Paths;
import java.util.List;

import org.apache.jena.ext.com.google.common.io.Files;
import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.ArgumentMaps;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.result.ResultStream;

public class H2Source extends JDBCSource {

    private final String mapFolder;

    public H2Source(String mapPath, String path) {
        super("org.h2.Driver","jdbc:h2:" + path, "user", "pass");

        this.mapFolder = (mapPath == null)
            ? null
            : getParentFolder(mapPath);
    }

    public H2Source(String mapPath) {
        this(mapPath, Files.createTempDir().getAbsolutePath() + "/H2Source");
    }

    public H2Source() {
        this(null);
    }

    @Override
    public ResultStream<List<String>> execute(String query) {
        return super.execute(setPWD(query));
    }

    @Override
    public ResultStream<ArgumentList> execute(String query, ArgumentMaps<String> argumentMaps) {
        return super.execute(setPWD(query), argumentMaps);
    }

    private String getParentFolder(String file) {
        return Paths.get(file).getParent().toAbsolutePath().toString();
    }

    private String setPWD(String query) {
        return (this.mapFolder == null)
            ? query
            : query.replaceAll(BOTTR.THIS_DIR, this.mapFolder);
    }
}