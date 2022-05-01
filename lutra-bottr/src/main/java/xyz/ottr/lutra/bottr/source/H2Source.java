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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.apache.jena.ext.com.google.common.io.Files;
import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.ArgumentMaps;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.system.ResultStream;

public class H2Source extends JDBCSource {

    private final Optional<String> mapFolder;

    public H2Source(String mapPath, String path) {
        super("org.h2.Driver","jdbc:h2:" + path, "user", "pass");
        this.mapFolder = getParentFolder(mapPath);
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
    public ResultStream<List<Argument>> execute(String query, ArgumentMaps<String> argumentMaps) {
        return super.execute(setPWD(query), argumentMaps);
    }

    private Optional<String> getParentFolder(String file) {
        return Optional.ofNullable(file)
            .map(Paths::get)
            .map(Path::getParent)
            .map(Path::toAbsolutePath)
            .map(Path::toString);
    }

    private String setPWD(String query) {
        return this.mapFolder
            .map(f -> query.replaceAll(BOTTR.THIS_DIR, f))
            .orElse(query);
    }
}