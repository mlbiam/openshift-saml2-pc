package com.tremolosecurity.unison.k8s.tasks;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

import com.tremolosecurity.provisioning.core.ProvisioningException;
import com.tremolosecurity.provisioning.core.User;
import com.tremolosecurity.provisioning.core.WorkflowTask;
import com.tremolosecurity.provisioning.util.CustomTask;
import com.tremolosecurity.saml.Attribute;

/**
 * NewAppTasks
 */
public class NewAppTasks implements CustomTask {

    @Override
    public boolean doTask(User user, Map<String, Object> request) throws ProvisioningException {
        String sshKey = ((String) request.get("gitPrivateKey"));
        try {
            String base64PrivKey = Base64.getEncoder().encodeToString(sshKey.getBytes("UTF-8"));
            request.put("base64SshPrivateKey", base64PrivKey);
        } catch (UnsupportedEncodingException e) {
            throw new ProvisioningException("Could get key",e);
        }
        
        String gitUrl = (String) request.get("gitSshUrl");
        String prefix = gitUrl.substring(0,gitUrl.indexOf("@") + 1);
        String suffix = gitUrl.substring(gitUrl.indexOf(":"));
        String newGitUrl = new StringBuilder().append(prefix).append("gitlab-gitlab-shell.gitlab.svc.cluster.local").append(suffix).toString();

        request.put("gitSshInternalURL",newGitUrl);

        return true;
    }

    @Override
    public void init(WorkflowTask task, Map<String, Attribute> config) throws ProvisioningException {

	}

	@Override
	public void reInit(WorkflowTask task) throws ProvisioningException {
		
	}

    
}