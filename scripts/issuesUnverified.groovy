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
def cFieldValue = issue.getCustomFieldValue(fixVer)
if (!cFieldValue) {
    return '0'
}
def myJQL = "project = " + issue.projectObject.key + " and fixVersion = " + cFieldValue[0]
def query = jqlQueryParser.parseQuery(myJQL)
def results = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter())

def Unverified = 0

results.getIssues().each {documentIssue ->
    def issue = issueManager.getIssueObject(documentIssue.id)    
    if ((issue.getStatusObject().name != "Closed") && (issue.getStatusObject().name != "Verified")) {
        Unverified = Unverified + 1
    }
}

cFieldValue = issue.getCustomFieldValue(inclVer)
if (cFieldValue) {
    def verlist = ""
    for (v in cFieldValue) {
        verlist = verlist + v.toString() + ","
    }
    verlist = verlist.substring(0, verlist.length()-1)
    
    myJQL = "project = " + issue.projectObject.key + " and fixVersion in (" + verlist + ")"
    query = jqlQueryParser.parseQuery(myJQL)
    results = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter())
    
    results.getIssues().each {documentIssue ->
        def issue = issueManager.getIssueObject(documentIssue.id)
        if ((issue.getStatusObject().name != "Closed") && (issue.getStatusObject().name != "Verified")) {
            Unverified = Unverified + 1
        }
    }
}
return Unverified.toString()
