# Presentation helper

A simple tool to help presenting with a git repository

Usage:
 - CheckConfig can be used to check the validity of the `application.conf` (and potentially the `-D` VM arg overrides)
 - App can be used to show the current branch information for the specified repository

Example `application.conf` (the hidden branches will hide the display):

```hocon
repository = "../../IdeaProjects/free-tagless-compare"
known-branches = [
    { name = zio},
    { name = master},
    { name = vacation, state.type = hidden },
]
```