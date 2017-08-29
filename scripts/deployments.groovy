import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.web.bean.PagerFilter

def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser.class)
def searchProvider = ComponentAccessor.getComponent(SearchProvider.class)
def issueManager = ComponentAccessor.getIssueManager()
def user = ComponentAccessor.getJiraAuthenticationContext().getUser()
def it = issue.getIssueTypeObject().getName()
def customFieldManager = ComponentAccessor.getCustomFieldManager()
if ((it != "New Feature") && (it != "Improvement") && (it != "Bug")) {
    return ''
}
def fixVer = issue.getFixVersions();
if (!fixVer[0]) {
    return ''
}

def myJQL = "project = " + issue.projectObject.key + " AND ('Versions included in deployment' = '" + fixVer[0] + "' or 'Deployment Version' = '" + fixVer[0] + "')"
def query = jqlQueryParser.parseQuery(myJQL)
def results = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter())

def span_blue = '<span class=" jira-issue-status-lozenge aui-lozenge jira-issue-status-lozenge-blue-gray jira-issue-status-lozenge-new jira-issue-status-lozenge-compact jira-issue-status-lozenge-max-width-medium" data-tooltip="<span class=&quot;jira-issue-status-tooltip-title&quot;>'
def span_yellow = '<span class=" jira-issue-status-lozenge aui-lozenge jira-issue-status-lozenge-yellow jira-issue-status-lozenge-indeterminate jira-issue-status-lozenge-compact jira-issue-status-lozenge-max-width-medium" data-tooltip="<span class=&quot;jira-issue-status-tooltip-title&quot;>'
def span_green = '<span class=" jira-issue-status-lozenge aui-lozenge jira-issue-status-lozenge-green jira-issue-status-lozenge-done jira-issue-status-lozenge-compact jira-issue-status-lozenge-max-width-medium" data-tooltip="<span class=&quot;jira-issue-status-tooltip-title&quot;>'
def span_gray = '<span class=" jira-issue-status-lozenge aui-lozenge jira-issue-status-lozenge-red jira-issue-status-lozenge-done jira-issue-status-lozenge-compact jira-issue-status-lozenge-max-width-medium" data-tooltip="<span class=&quot;jira-issue-status-tooltip-title&quot;>'
def span_end = '</span>" original-title="">&nbsp;</span>'

def html = '<table cellspacing="5">'
results.getIssues().each {documentIssue ->
    def issue = issueManager.getIssueObject(documentIssue.id)
    def img = '<IMG SRC="http://jira/secure/viewavatar?size=xsmall&avatarId=13312&avatarType=issuetype">'
    
    def region = customFieldManager.getCustomFieldObject("customfield_10806")
    def my_region = issue.getCustomFieldValue(region)
    // Deteremine Issue
    def status = issue.getStatusObject().name;
    def span = span_yellow + status + span_end;
    if (status == "Closed") {
        //def resolution = issue.getResolution().name;
        def resolution = issue.getResolutionObject().name;
        if (resolution == "Cancelled") {
            span = span_gray + resolution + span_end;
        } else {
            span = span_green + resolution + span_end;
        }
    } else if ((status == "Change Review") || (status == "Open")  || (status == "Pending Approval")) {
        span = span_blue + status + span_end;
    }
    html = html + '<tr><td>' + img + '</td>'
    html = html + '<td width="100"><A HREF="http://jira/browse/' + issue + '" target="_new">' + issue + '</A></td>'
    html = html + '<td>' + my_region + '</td>'    
    html = html + '<td>&nbsp;</td><td>' + issue.getSummary() + '</td><td>' + span + '</td><td>' + status + '</td></tr>' 
    
}
html = html + '</table>'
return html
