import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.web.bean.PagerFilter
 
def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser.class)
def searchProvider = ComponentAccessor.getComponent(SearchProvider.class)
def issueManager = ComponentAccessor.getIssueManager()
def user = ComponentAccessor.getJiraAuthenticationContext().getUser()
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def it = issue.getIssueTypeObject().getName()
if (it != "Deployment") {
    return ''
}

def fixVer = customFieldManager.getCustomFieldObject("customfield_10101")
def inclVer = customFieldManager.getCustomFieldObject("customfield_10801")
def fix_version = issue.getCustomFieldValue(fixVer)
def included_version = issue.getCustomFieldValue(inclVer)
if ((!fix_version) && (!included_version)) {
    return 'None'
}
def myJQL = "project = " + issue.projectObject.key + " and fixVersion = " + fix_version[0]
def query = jqlQueryParser.parseQuery(myJQL)
def results = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter())

def span_blue = '<span class=" jira-issue-status-lozenge aui-lozenge jira-issue-status-lozenge-blue-gray jira-issue-status-lozenge-new jira-issue-status-lozenge-compact jira-issue-status-lozenge-max-width-medium" data-tooltip="<span class=&quot;jira-issue-status-tooltip-title&quot;>'
def span_yellow = '<span class=" jira-issue-status-lozenge aui-lozenge jira-issue-status-lozenge-yellow jira-issue-status-lozenge-indeterminate jira-issue-status-lozenge-compact jira-issue-status-lozenge-max-width-medium" data-tooltip="<span class=&quot;jira-issue-status-tooltip-title&quot;>'
def span_green = '<span class=" jira-issue-status-lozenge aui-lozenge jira-issue-status-lozenge-green jira-issue-status-lozenge-done jira-issue-status-lozenge-compact jira-issue-status-lozenge-max-width-medium" data-tooltip="<span class=&quot;jira-issue-status-tooltip-title&quot;>'
def span_end = '</span>" original-title="">&nbsp;</span>'
def html = '<table cellspacing="5"><tr><td colspan="4">Issues fixed in Version</td></tr>'
def Unverified = 0

results.getIssues().each {documentIssue ->
    def issue = issueManager.getIssueObject(documentIssue.id)
    
    def img = '<IMG SRC="http://jira/images/icons/issuetypes/newfeature.png">'
    if (issue.getIssueTypeId() == "1") {
        img = '<IMG SRC="http://jira/images/icons/bug.gif">'
    } else if (issue.getIssueTypeId() == "4") {
        img = '<IMG SRC="http://jira/images/icons/issuetypes/improvement.png">'
    } 
    // Deteremine Issue
    def span = span_yellow + issue.getStatusObject().name + span_end
    if ((issue.getStatusObject().name == "Closed") || (issue.getStatusObject().name == "Verified")) {
        span = span_green + issue.getStatusObject().name + span_end
    } else if ((issue.getStatusObject().name == "In Backlog") || (issue.getStatusObject().name == "Open")  || (issue.getStatusObject().name == "Reopened")) {
        span = span_blue + issue.getStatusObject().name + span_end
    }
    
    // Determine Unverified Issue Count
    if ((issue.getStatusObject().name != "Closed") && (issue.getStatusObject().name != "Verified")) {
        Unverified = Unverified + 1
    }
    html = html + '<tr><td>' + img + '</td>'
    html = html + '<td width="100"><A HREF="http://jira/browse/' + issue + '" target="_new">' + issue + '</A></td>'
    html = html + '<td>&nbsp;</td><td>' + issue.getSummary() + '</td><td>' + span + '</td></tr>' 
}

if (included_version) {
    def verlist = ""
    for (v in included_version) {
        verlist = verlist + v.toString() + ","
    }
    verlist = verlist.substring(0, verlist.length()-1)
    
    myJQL = "project = " + issue.projectObject.key + " and fixVersion in (" + verlist + ")"
    query = jqlQueryParser.parseQuery(myJQL)
    results = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter())
    
    html = html + '<table cellspacing="5"><tr><td colspan="4">Issues included in Version</td></tr>'
    results.getIssues().each {documentIssue ->
        def issue = issueManager.getIssueObject(documentIssue.id)
    
        // Determine Issuetype
        def img = '<IMG SRC="http://jira/images/icons/issuetypes/newfeature.png">'
        if (issue.getIssueTypeId() == "1") {
            img = '<IMG SRC="http://jira/images/icons/bug.gif">'
        } else if (issue.getIssueTypeId() == "4") {
            img = '<IMG SRC="http://jira/images/icons/issuetypes/improvement.png">'
        } 
        // Deteremine Issue
        def span = span_yellow + issue.getStatusObject().name + span_end
        if ((issue.getStatusObject().name == "Closed") || (issue.getStatusObject().name == "Verified")) {
            span = span_green + issue.getStatusObject().name + span_end
        } else if ((issue.getStatusObject().name == "In Backlog") || (issue.getStatusObject().name == "Open")  || (issue.getStatusObject().name == "Reopened")) {
            span = span_blue + issue.getStatusObject().name + span_end
        }
        // Determine Unverified Issue Count
        if ((issue.getStatusObject().name != "Closed") && (issue.getStatusObject().name != "Verified")) {
            Unverified = Unverified + 1
        }
        html = html + '<tr><td>' + img + '</td>'
        html = html + '<td width="100"><A HREF="http://jira/browse/' + issue + '" target="_new">' + issue + '</A></td>'
        html = html + '<td>&nbsp;</td><td>' + issue.getSummary() + '</td><td>' + span + '</td></tr>' 
    }
}

html = html + '</table>'
return html
