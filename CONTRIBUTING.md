# Contributing

Contributions are very welcome. The following will provide some helpful guidelines.

## Follow the Code of Conduct

Contributors must follow the [Code of Conduct](CODE-OF-CONDUCT.md).

## How to contribute

We love pull requests. Here is a quick guide:

1. Fork the repo (see https://help.github.com/articles/fork-a-repo).
1. Create a new branch from master.
1. Add your change, verify it by testing on an emulated devices or your local Android device.
1. Run `./gradlew build`
1. Add your change together with a test (tests are not needed for refactorings and documentation changes).
1. Create a Pull Request

### Commits

Commit messages should be clear and fully elaborate the context and the reason of a change.
If your commit refers to an issue, please post-fix it with the issue number, e.g.

```
Issue: #123
```

Furthermore, commits should be signed off according to the [DCO](DCO.md).

### Pull Requests

If your Pull Request resolves an issue, please add a respective line to the end, like

```
Resolves #123
```

### Deployment

Can be only done by the repository owner. Deployment is done via Github actions.

[![Actions Panel](https://img.shields.io/badge/actionspanel-enabled-brightgreen)](https://www.actionspanel.app/app/klassm/andFHEM)
