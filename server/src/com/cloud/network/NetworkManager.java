// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.network;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.cloudstack.acl.ControlledEntity.ACLType;

import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapcityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.element.DhcpServiceProvider;
import com.cloud.network.element.LoadBalancingServiceProvider;
import com.cloud.network.element.StaticNatServiceProvider;
import com.cloud.network.element.UserDataServiceProvider;
import com.cloud.network.guru.NetworkGuru;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.utils.Pair;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicVO;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Type;
import com.cloud.vm.VirtualMachineProfile;

/**
 * NetworkManager manages the network for the different end users.
 * 
 */
public interface NetworkManager  {


    List<NetworkVO> setupNetwork(Account owner, NetworkOffering offering, DeploymentPlan plan, String name, String displayText, boolean isDefault)
            throws ConcurrentOperationException;

    List<NetworkVO> setupNetwork(Account owner, NetworkOffering offering, Network predefined, DeploymentPlan plan, String name, String displayText, boolean errorIfAlreadySetup, Long domainId,
            ACLType aclType, Boolean subdomainAccess, Long vpcId, Boolean isDisplayNetworkEnabled) throws ConcurrentOperationException;

    void allocate(VirtualMachineProfile vm, LinkedHashMap<? extends Network, ? extends NicProfile> networks) throws InsufficientCapacityException, ConcurrentOperationException;

    void prepare(VirtualMachineProfile profile, DeployDestination dest, ReservationContext context) throws InsufficientCapacityException, ConcurrentOperationException,
            ResourceUnavailableException;

    void release(VirtualMachineProfile vmProfile, boolean forced) throws
			ConcurrentOperationException, ResourceUnavailableException;

    void cleanupNics(VirtualMachineProfile vm);

    void expungeNics(VirtualMachineProfile vm);

    List<NicProfile> getNicProfiles(VirtualMachine vm);

    Pair<NetworkGuru, NetworkVO> implementNetwork(long networkId, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException,
            InsufficientCapacityException;

    /**
     * prepares vm nic change for migration
     * 
     * This method will be called in migration transaction before the vm migration.
     * @param vm
     * @param dest
     */
    void prepareNicForMigration(VirtualMachineProfile vm, DeployDestination dest);

    /**
     * commit vm nic change for migration
     * 
     * This method will be called in migration transaction after the successful
     * vm migration.
     * @param src
     * @param dst
     */
    void commitNicForMigration(VirtualMachineProfile src, VirtualMachineProfile dst);

    /**
     * rollback vm nic change for migration
     * 
     * This method will be called in migaration transaction after vm migration
     * failure.
     * @param src
     * @param dst
     */
    void rollbackNicForMigration(VirtualMachineProfile src, VirtualMachineProfile dst);

    boolean shutdownNetwork(long networkId, ReservationContext context, boolean cleanupElements);

    boolean destroyNetwork(long networkId, ReservationContext context);

    Network createGuestNetwork(long networkOfferingId, String name, String displayText, String gateway, String cidr,
                               String vlanId, String networkDomain, Account owner, Long domainId, PhysicalNetwork physicalNetwork,
                               long zoneId, ACLType aclType, Boolean subdomainAccess, Long vpcId, String ip6Gateway, String ip6Cidr,
                               Boolean displayNetworkEnabled, String isolatedPvlan)
                    throws ConcurrentOperationException, InsufficientCapacityException, ResourceAllocationException;

    UserDataServiceProvider getPasswordResetProvider(Network network);

    UserDataServiceProvider getSSHKeyResetProvider(Network network);

    boolean startNetwork(long networkId, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    boolean reallocate(VirtualMachineProfile vm, DataCenterDeployment dest) throws InsufficientCapacityException, ConcurrentOperationException;

    /**
     * @param requested
     * @param network
     * @param isDefaultNic
     * @param deviceId
     * @param vm
     * @return
     * @throws InsufficientVirtualNetworkCapcityException
     * @throws InsufficientAddressCapacityException
     * @throws ConcurrentOperationException
     */
    Pair<NicProfile,Integer> allocateNic(NicProfile requested, Network network, Boolean isDefaultNic, int deviceId,
            VirtualMachineProfile vm) throws InsufficientVirtualNetworkCapcityException,
            InsufficientAddressCapacityException, ConcurrentOperationException;


    /**
     * @param vmProfile
     * @param dest
     * @param context
     * @param nicId
     * @param network
     * @return
     * @throws InsufficientVirtualNetworkCapcityException
     * @throws InsufficientAddressCapacityException
     * @throws ConcurrentOperationException
     * @throws InsufficientCapacityException
     * @throws ResourceUnavailableException
     */
    NicProfile prepareNic(VirtualMachineProfile vmProfile, DeployDestination dest,
            ReservationContext context, long nicId, NetworkVO network) throws InsufficientVirtualNetworkCapcityException,
            InsufficientAddressCapacityException, ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException;


    /**
     * @param vm
     * @param nic TODO
     */
    void removeNic(VirtualMachineProfile vm, Nic nic);


    /**
     * @param network
     * @param provider
     * @return
     */
    boolean setupDns(Network network, Provider provider);


    /**
     * @param vmProfile
     * @param nic TODO
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    void releaseNic(VirtualMachineProfile vmProfile, Nic nic)
            throws ConcurrentOperationException, ResourceUnavailableException;


    /**
     * @param network
     * @param requested
     * @param context
     * @param vmProfile
     * @param prepare TODO
     * @return
     * @throws InsufficientVirtualNetworkCapcityException
     * @throws InsufficientAddressCapacityException
     * @throws ConcurrentOperationException
     * @throws InsufficientCapacityException
     * @throws ResourceUnavailableException
     */
    NicProfile createNicForVm(Network network, NicProfile requested, ReservationContext context, VirtualMachineProfile vmProfile, boolean prepare)
            throws InsufficientVirtualNetworkCapcityException,
            InsufficientAddressCapacityException, ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException;


    NetworkProfile convertNetworkToNetworkProfile(long networkId);

    /**
     * @return
     */
    int getNetworkLockTimeout();
    

    boolean restartNetwork(Long networkId, Account callerAccount,
            User callerUser, boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;


    boolean shutdownNetworkElementsAndResources(ReservationContext context,
            boolean b, NetworkVO network);


	void implementNetworkElementsAndResources(DeployDestination dest,
			ReservationContext context, NetworkVO network,
			NetworkOfferingVO findById) throws ConcurrentOperationException, InsufficientAddressCapacityException, ResourceUnavailableException, InsufficientCapacityException;


	Map<String, String> finalizeServicesAndProvidersForNetwork(NetworkOffering offering,
			Long physicalNetworkId);

    List<Provider> getProvidersForServiceInNetwork(Network network, Service service);

    StaticNatServiceProvider getStaticNatProviderForNetwork(Network network);
    
    boolean isNetworkInlineMode(Network network);

    LoadBalancingServiceProvider getLoadBalancingProviderForNetwork(Network network, Scheme lbScheme);

    boolean isSecondaryIpSetForNic(long nicId);

    List<? extends Nic> listVmNics(Long vmId, Long nicId);
    
    NicVO savePlaceholderNic(Network network, String ip4Address, String ip6Address, Type vmType);

    DhcpServiceProvider getDhcpServiceProvider(Network network);
    void removeDhcpServiceInSubnet(NicVO nic);
}
