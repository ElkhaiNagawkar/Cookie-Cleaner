# Cookie Cleaner - Burp Suite Extension

Cookie Cleaner is a Burp Suite extension that gives you full control over which cookies are sent with your requests, allowing you to filter out noise by cleaning irrelevant cookies.

### Features
  - **Scan** - Scan your proxy history to populate a table with all in scope cookies eligible to be filtered.
  - **Filtering** - Move cookies to a "Removed Cookies" table that will filter out the specified cookies from your reqeusts.
  - **Active Clean** - Choose whether to actively remove cookies when proxying your reqeusts, cleaning up cookies from your reqeusts in real time.
  - **Manual Cookie Cleaning** - When in Intercept or Repeater use the Context Menu to manualy clean cookies from your reqeusts. Cookies specified in the "Removed Cookies" table will be removed from the selected reqeust
  - **Manual Cookie Addition** - If a cookie has not been scanned by the extension or a cookie has not been moved to the "Removed Cookies" table, you can manually do so by hightlighting the cookie in your reqeust and using the context menu to add it to be filtered out.
  - **Multiple Cookie Addition** - You may manually select multiple cookies to be added to the extension to be removed. Do this by highlighting full cookies (name + value) or the full Cookie header.
