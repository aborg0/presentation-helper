![Scala CI](https://github.com/aborg0/presentation-helper/workflows/Scala%20CI/badge.svg)

# Presentation helper

A simple tool to help present with a git repository

Usage:
 - CheckConfig can be used to check the validity of the `application.conf` (and potentially the `-D` VM arg overrides)
 - App can be used to show the current branch information for the specified repository

Example `application.conf` (the hidden branches will hide the display):

```hocon
repository = "../../IdeaProjects/free-tagless-compare"
style = light
known-branches = [
    { name = zio},
    { name = master, description = "Main branch"},
    { name = vacation, display-name = "???", state.type = hidden },
]
```

You can override for example the repository with the VM arg `-Drepository=.` setting.