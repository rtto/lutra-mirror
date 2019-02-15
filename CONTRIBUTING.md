
Feel free to create an issue and/or send a pull request (PR).

Make sure that your [pull request][5] is submitted to the `develop` branch
or suitable feature branch (`feature/*`), explaining what your PR is
about and, preferably, linking to an issue you want to address. It is
always a good idea to create an issue first where you can check if the
issue is real and your possible solution proposal is likely to be
accepted if implemented.

Please document your code using common documentation practice and
javadoc, and test your code with unit tests.

This is an open source project; please be nice, respectful and
helpful.

## Git branches

We use the following git branching model:
https://nvie.com/posts/a-successful-git-branching-model/ with the
following branch types:

- master
- develop
- release/*
- feature/*
- hotfix/*

We tag each release with `v` + its version number, e.g., `v0.5.0`.

## Git commit messages

- Use the present tense ("Add feature" not "Added feature")
- Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
- Limit the first line to 72 characters or less
- Reference issues and pull requests liberally after the first line
- When only changing documentation, include [ci skip] in the commit title

(Taken from https://github.com/atom/atom/blob/master/CONTRIBUTING.md#git-commit-messages)

## Automatic builds

The code base is built for each commit. The results of the build are
kept for the `master` and `develop` branch, and for each tag. See
[README](README.md) for links.

## Styleguide

We use Checkstyle http://checkstyle.sourceforge.net/ to enforce a
common coding style. We are not religious to about this, but use it to
make the code as consistent as possible between different coders.

The style rules are set in config/checkstyle.xml and are checked by a
Maven plugin on `mvn package`.

## Code quality

We use different automatic code quality checks.

- PDM (https://pmd.github.io/) and
- FindBugs (http://findbugs.sourceforge.net/)

are included via Maven plugins and are checked on `mvn package install`.

We also use:

- Gitlab's Code Climate integration (https://docs.gitlab.com/ee/user/project/merge_requests/code_quality.html) and
- Codebeat (https://codebeat.co/projects/gitlab-com-ottr-lutra-lutra-develop)

to identify potential weak spots in the code.

## License

See [README](README.md).