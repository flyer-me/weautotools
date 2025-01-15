
namespace CloudWork.Service.Interface
{
    public interface IGenericService<TEntity> where TEntity : class
    {
        /// <summary>
        /// id 获取实体
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>
        Task<TEntity?> GetByIdAsync(object id);

        /// <summary>
        /// 获取所有实体
        /// </summary>
        /// <returns>实体枚举</returns>
        Task<IEnumerable<TEntity>> GetAllAsync();
        /// <summary>
        /// 添加实体
        /// </summary>
        /// <param name="entity"></param>
        /// <returns>Task 影响行数</returns>
        Task AddAsync(TEntity entity);
        /// <summary>
        /// 更新实体
        /// </summary>
        /// <param name="entity"></param>
        /// <returns>Task 影响行数</returns>
        void Update(TEntity entity);
        /// <summary>
        /// 删除实体
        /// </summary>
        /// <param name="id"></param>
        /// <returns>Task 影响行数</returns>
        Task DeleteAsync(object id);
        /// <summary>
        /// 保存到数据库
        /// </summary>
        /// <returns></returns>
        Task<int> SaveAsync();
    }
}
