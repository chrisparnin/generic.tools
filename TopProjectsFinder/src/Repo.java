
public class Repo {

	String status;
	String password;
	String username;
	String module;
	String url;
	String type;

	public Repo(String type, String repoURL, String module, String username,
			String password, String status) {
		
		this.type = type;
		this.url = repoURL;
		this.module = module;
		this.username = username;
		this.password = password;
		this.status = status;
		
	}

}
