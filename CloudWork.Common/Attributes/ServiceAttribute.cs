using Microsoft.Extensions.DependencyInjection;

namespace CloudWork.Common.Attributes
{
    /// <summary>
    /// 用于配置服务生命周期：Service(Scoped(默认), Singleton, Transient)
    /// </summary>
    [AttributeUsage(AttributeTargets.Class)]
    public class ServiceAttribute : Attribute
    {
        public ServiceAttribute(ServiceLifetime lifetime = ServiceLifetime.Scoped)
        {
            Lifetime = lifetime;
        }
        public ServiceLifetime Lifetime { get; set; } = ServiceLifetime.Scoped;

    }
}
