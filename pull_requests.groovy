@Grapes([
	@Grab(group='joda-time', module='joda-time', version='2.1'),
	@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7' )])
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import org.joda.time.*
import org.joda.time.format.*

DateTimeFormatter inputfmt = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC) 

def http = new HTTPBuilder( 'https://api.github.com/' )
def header_accept = 'application/json' 
def codice_repos = ''

http.request(GET,JSON) { req ->
  uri.path = 'orgs/codice/repos'
  uri.query = [type: 'source']
  headers.'Accept' = header_accept
  // Requires a user-agent
  headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
 
  response.success = { resp, json ->
    assert resp.status == 200
    repoList = json
  }
}

println "Found ${repoList.size} repos."
repoList.each {
	def repoName = it.name
	def issueNum = it.open_issues_count
	if (issueNum > 0) {
		def pullInfo
		println "Repo ${repoName} has ${issueNum} open pull request(s):"
		http.request(GET,JSON) { req ->
		  uri.path = "repos/codice/${repoName}/pulls"
		  uri.query = [type: 'source']
		  headers.'Accept' = header_accept
		  headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
 
		  response.success = { resp, json ->
		    assert resp.status == 200
		    pullInfo = json
		  }
		}
		pullInfo.each {
			DateTime createdDate = inputfmt.parseDateTime(it.created_at)
			Period period = new Period(createdDate, new DateTime(), PeriodType.yearWeekDay())
			println "[${it.title}] submitted by user <${it.user.login}> ${PeriodFormat.getDefault().print(period)} ago."
		}
	}
}