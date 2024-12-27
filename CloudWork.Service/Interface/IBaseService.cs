
namespace CloudWork.Service.Interface
{
    public interface IBaseService<TEntity> where TEntity : class
    {
        /// <summary>
        /// id 获取实体
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>
        Task<TEntity?> GetByIdAsync(int id);

        /// <summary>
        /// 获取所有实体
        /// </summary>
        /// <returns>实体枚举</returns>
        Task<IEnumerable<TEntity>> GetAllAsync();
        /// <summary>
        /// 添加实体到数据库
        /// </summary>
        /// <param name="entity"></param>
        /// <returns>Task 影响行数</returns>
        Task<int> AddAsync(TEntity entity);
        /// <summary>
        /// 更新实体到数据库
        /// </summary>
        /// <param name="entity"></param>
        /// <returns>Task 影响行数</returns>
        Task<int> UpdateAsync(TEntity entity);
        /// <summary>
        /// 删除实体到数据库
        /// </summary>
        /// <param name="id"></param>
        /// <returns>Task 影响行数</returns>
        Task<int> DeleteAsync(int id);
    }
}
