package com.tremolosecurity.unison.k8s.tasks;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import com.tremolosecurity.provisioning.core.ProvisioningException;
import com.tremolosecurity.provisioning.core.User;
import com.tremolosecurity.provisioning.core.WorkflowTask;
import com.tremolosecurity.provisioning.util.CustomTask;
import com.tremolosecurity.saml.Attribute;
import com.tremolosecurity.server.GlobalEntries;
import com.tremolosecurity.unison.openshiftv3.OpenShiftTarget;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * NewAppTasks
 */
public class NewAppTasks implements CustomTask {
    String target;

    @Override
    public boolean doTask(User user, Map<String, Object> request) throws ProvisioningException {
        String projectName = (String) request.get("projectName");
        String sshKey = ((String) request.get("gitPrivateKey"));

        if (! sshKey.endsWith("\n")) {
            sshKey += "\n";
        }

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

        String secretData = DigestUtils.sha256Hex(UUID.randomUUID().toString());

        request.put("webHookSecretData",secretData);
       

        OpenShiftTarget oc = (OpenShiftTarget) GlobalEntries.getGlobalEntries().getConfigManager().getProvisioningEngine().getTarget(this.target).getProvider();
        request.put("weebhookURL",oc.getUrl() + "/oapi/v1/namespaces/jenkins/buildconfigs/" + projectName + "/webhooks/" + secretData + "/gitlab");
        return true;
    }

    @Override
    public void init(WorkflowTask task, Map<String, Attribute> config) throws ProvisioningException {
        this.target = config.get("target").getValues().get(0);
	}

	@Override
	public void reInit(WorkflowTask task) throws ProvisioningException {
		
	}

    
}