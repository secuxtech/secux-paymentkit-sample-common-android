# Credential handling

## Remaining payment credential cleanup

- The reported password exists only in historical source. Current source obtains merchant credentials from runtime settings. Removed the checked-in old APK to avoid continuing to distribute an opaque older build; current runtime credential flow is preserved.

This cleanup removes the identified values/artifacts from the current branch. It does not revoke server accounts, rotate deployed device keys, rewrite Git history, remove old tags/releases, or automatically resolve scanner alerts. Treat previously published usable credentials as exposed and have their owners revoke or rotate them. Use new, narrowly scoped test credentials if development resumes.
