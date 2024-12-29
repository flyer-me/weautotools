using Microsoft.EntityFrameworkCore.ChangeTracking;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using System.Reflection;

namespace CloudWork.Common.Extensions
{
    public static class ServiceRegistrationExtensions
    {
        /// <summary>
        /// 注册程序集中所有标记了 <see cref="ServiceAttribute"/> 的服务到依赖注入容器中，不包括泛型。
        /// 根据 <see cref="ServiceAttribute"/> 中指定的服务生命周期注册。
        /// </summary>
        /// <param name="services">依赖注入容器 (<see cref="IServiceCollection"/>)，用于注册发现的服务。</param>
        /// <remarks>
        /// - 如果同一个接口被多个服务实现，则最后一个注册的服务将覆盖之前的注册。
        /// </remarks>
        public static void RegisterByServiceAttribute(this IServiceCollection services)
        {
            var types = GetAllAssembly().SelectMany(x => x.DefinedTypes)
                .Where(t => t.IsClass && !t.IsAbstract
                && t.GetCustomAttributes(typeof(ServiceAttribute), false).Length > 0
                && !t.IsGenericType);

            foreach (var type in types)
            {
                var lifeTime = type.GetCustomAttribute<ServiceAttribute>()!.Lifetime;
                var baseInterfaces = type.BaseType?.GetInterfaces() ?? Enumerable.Empty<Type>();
                var interfaces = type.ImplementedInterfaces.Except(baseInterfaces);

                foreach (var i in interfaces)
                {
                    services.Add(new ServiceDescriptor(i, type, lifeTime));

                    Console.WriteLine($"注册 {lifeTime}: {i.Name} => {type.Name}");

                    /*switch (lifeTime)
                    {
                        case ServiceLifetime.Singleton:
                            services.AddSingleton(i, type); break;
                        case ServiceLifetime.Transient:
                            services.AddTransient(i, type); break;
                        case ServiceLifetime.Scoped:
                            services.AddScoped(i, type);    break;
                        default:
                            services.AddScoped(i, type);    break;
                    }*/
                }
            }

        }

        /// <summary>
        /// 获取全部 Assembly
        /// </summary>
        /// <returns></returns>
        private static List<Assembly> GetAllAssembly()
        {

            var allAssemblies = AppDomain.CurrentDomain.GetAssemblies().ToList();

            HashSet<string> loadedAssemblies = new();

            foreach (var item in allAssemblies)
            {
                loadedAssemblies.Add(item.FullName!);
            }

            Queue<Assembly> assembliesToCheck = new();
            assembliesToCheck.Enqueue(Assembly.GetEntryAssembly()!);

            while (assembliesToCheck.Any())
            {
                var assemblyToCheck = assembliesToCheck.Dequeue();
                foreach (var reference in assemblyToCheck!.GetReferencedAssemblies())
                {
                    if (!loadedAssemblies.Contains(reference.FullName))
                    {
                        var assembly = Assembly.Load(reference);

                        assembliesToCheck.Enqueue(assembly);

                        loadedAssemblies.Add(reference.FullName);

                        allAssemblies.Add(assembly);
                    }
                }
            }

            return allAssemblies;
        }

    }
}
