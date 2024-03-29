If you have questions/feedback/bug reports/feature requests, use the 
issue tracker. Please check first that the issue does not already exist. 
See also http://ottr.xyz/#Contact for more options.

If you want to contribute to the project by coding or writing documentation, 
please fork the project and submit your contributions via merge requests (MR) to
the `develop` branch
or suitable feature branch (`feature/*`) where you explain what your MR is about.  
If no suitable feature branch exist, you can ask for one to be created.
In the description of the MR, please link to the issue(s) your contribution addresses. If no issue exists, it can be a good idea to create an issue first
in order to start a discussion with other developers about if and how the issue should be solved.
The MR will be reviewed by the project's maintainers.

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
- release/[version number]
- feature/[descriptive name]
- hotfix/[descriptive name]

The basic rule is that contributions are put in a branch named `feature/[descriptive name]` created from the `develop` branch, where `[descriptive name]` is a high-level description of the contribution. 

We tag each release with `v` + its version number, e.g., `v0.5.0`.

## Git commit messages

- Use the present tense ("Add feature" not "Added feature")
- Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
- Limit the first line to 72 characters or less
- Reference whenever possible issues and pull requests liberally after the first line
- When only changing documentation, include [ci skip] in the commit title

(Taken from https://github.com/atom/atom/blob/master/CONTRIBUTING.md#git-commit-messages)

## Automatic builds

The code base is built and tested for each commit. The results of the build are
kept for the `master` and `develop` branch, and for each tag. See
[README](README.md) for links.

## Styleguide

We use Checkstyle http://checkstyle.sourceforge.net/ to enforce a
common coding style. We are not religious to about this, but use it to
make the code as consistent as possible between different coders.

The style rules are set in `config/checkstyle.xml` and are checked by a
Maven plugin when running `mvn verify`.

## Code quality

We use different automatic code quality checks.

- PDM (https://pmd.github.io/) and
- FindBugs (http://findbugs.sourceforge.net/)

are included via Maven plugins and are checked when running `mvn verify`.

We also use:

- Gitlab's Code Climate integration (https://docs.gitlab.com/ee/user/project/merge_requests/code_quality.html) and
- Codebeat (https://codebeat.co/projects/gitlab-com-ottr-lutra-lutra-develop)

to identify potential weak spots in the code.

## License

See [README](README.md).
